![Alt text](gfx/jubako_logo.png?raw=true "Jubako Logo")

[![](https://jitpack.io/v/justeat/jubako.svg)](https://jitpack.io/#justeat/jubako)

<img align="left" src="https://github.com/justeat/jubako/blob/master/gfx/jubako_movies.png" />
Jubako makes things super simple to assemble rich content into a `RecyclerView` such as a wall of carousels (Google Play style recycler in recyclers). Jubako can load content on the fly asynchronously, infinitely with pagination.


## The simplest example - "Hello Jubako! x 100"
```kotlin
class HelloJubakoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jubako_recycler)

        recyclerView.withJubako(this).load {
            for (i in 0..100) {
                addView { textView("Hello Jubako!") }
                addView { textView("こんにちはジュバコ") }
            }
        }
    }

    private fun textView(text: String): TextView {
        return TextView(this).apply {
            setText(text)
        }
    }
}
```

In this simple example we use some of Jubako's convenience extensions to compose a `RecyclerView` with
100 rows.

Firstly the extension function `RecyclerView.withJubako` expresses which RecyclerView we want to load into
(passing context) and then we follow up with a call to `load` describing what we want to load inside its lambda argument.
  
We can then make calls to Jubako's `withView` extension function to specify each view (just a regular `android.view.View`)  we wish
to display for a row in our recycler that conveniently constructs the necessary boilerplate under the hood.

In the example we just print out 100 rows of static content, but Jubako was built to do much more than that.

**Mostly this approach might work for simple applications, but under the hood Jubako offers more verbose
construction to support more complicated scenarios.** 

The best place to start right now with Jubako is to check it the examples in the `jubako-sample` app in this repository.

## JubakoAssembler
Without the added convenience of Jubako `load` we can also load content with a derived implementation of `JubakoAssembler`. 

An assembler (similar to an adapter) is used to compose a **list of descriptions** that we wish to render (carousels, cards, etc). 
Its basic interface has a single function `::assemble()` that will be called by Jubako when it is time to assemble this list.

Content is added with the assembler by creating and adding instances of `ContentDescriptionProvider`, and the purpose of a provider
is to construct an instance of `ContentDescription` where a content description defines which view holder to use and the data that
will be bound to the view holder where that data is a `LiveData<T>`.

The simplest usage of `JubakoAssembler` is using its derived type `SimpleJubakoAssembler` that adds convenience to
assembling simple lists of content, for example:-

```
val assembler = SimpleJubakoAssembler {
    add(HelloDescriptionProvider())
}
```

Jubako will call `JubakoAssembler::assemble()` asynchronously (via coroutines) and this will give your implementation
the chance to perform initialisation work such as fetching data in order to construct this **list of descriptions**.

You can tell Jubako that your assembler produces even more content if `::assemble` is called again
by implementing `JubakoAssembler::hasMore` you can control how much more content you want Jubako to consume
by returning `true` or `false` for more or no more content respectively.

Jubako's OOTB `PaginatedContentLoadingStrategy` will take care of loading more when demanded.

### Waiting for assembly to complete
When Jubako calls `JubakoAssembler::assemble` it will do so asynchronously which we refer to as the *Assembly Phase*

During assembly you can respond to state changes from *Assembling* to *Assembled* and *AssembleError*

The first state *Assembling* tells you that your `JubakoAssembler` is currently waiting for `assemble` to return
before it goes into the *Assembled* state. You can respond to these state changes when observing content as follows:-

```kotlin
Jubako.observe(this) { state ->
    when (state) {
        is Jubako.State.Assembled -> {
            recyclerView.adapter = JubakoAdapter(state.data)
        }
        is Jubako.State.Assembling -> {
            // TODO show a loading indicator
        }
        is Jubako.State.AssembleError -> {
            // TODO deal with the error
        }
    }
}
```

In the example above the common case for listening to `Assembling` is to show or hide loading indicators and handle
any exceptions from the call to `::assemble()`.

As well as these callbacks there is also a callback `onInitialFill` on `JubakoAdapter` that will be
called when Jubako initially fills the screen with content. This callback can also be used to hide any loading indicators
and show the content (the `RecyclerView`). The difference between this callback and `Jubako.State.Assembled`
is that it occurs after data has loaded and the screen is filled for the first time
where `Jubako.State.Assembled` when data is loaded.

If any of your content descriptions have live data that takes some time to load it may be more appropriate to
wait for the screen to fill by hooking into `onInitialFill` before transitioning from
loading indicators to showing content.

## ContentDescription
For each row in Jubako is a `ContentDescription` that defines which view holder to use and which data to bind (in the form of `LiveData<T>`).

With Jubako, when we assemble content for display using a `ContentAssembler`, we provide this content as a list of `ContentDescription` or more
precisely a list of `ContentDescriptionProvider` where a providers purpose is to produce a description.

A description has various properties of which some are required.

#### id: String (optional)
A unique ID that represents this row (use `UUID` to create one to keep things unique if a
specific name is not important)

#### viewHolderFactory: JubakoAdapter.HolderFactory<T> (required)
A factory class that will create a ViewHolder that you want to use when rendering.

#### data: LiveData<T>? (required)
The data that will be loaded where T can be any type, later on when rendering this data (when loaded) will be passed to your
`ViewHolder`'s `bind(T)` that you can implement to render the loaded content.

## JubakoAdapter
Once you observe the state `Jubako.State.Assembled` you can go ahead and construct your `JubakoAdapter`, by default the adapter will use `PaginatedContentLoadingStrategy`.

If you use `JubakoRecyclerView` then you will not need to set a layout manager (and not a good idea either since Jubako
currently supports only `LinearLayoutManager` in vertical orientation).


```kotlin
Jubako.observe(this) { state ->
    when (state) {
        is Jubako.State.Assembled -> {
            jubakoRecycler.adapter = JubakoAdapter(activity, state.data)
        }
    }
}
```

### Reloading
sometimes you might need to reload a `ContentDescription`, you can do this from
either within the `JubakoViewHolder` or from the `JubakoAdapter`

##### Reloading from JubakoViewHolder
The following example shows how you can call the `reload()` function from within
a `JubakoViewHolder`.

```kotlin
class ExampleViewHolder(view: View) : JubakoViewHolder<String>(view) {
    override fun bind(data: String?) {
        itemView.findViewById(R.id.error_button).setOnClickListener {
            reload()
        }
    }
}
```

#### Reloading from JubakoAdapter
Reloading from `JubakoAdapter` requires the `id` you assigned to the `ContentDescription`
when creating a given description, once this is done you can call reload, eg:-

```kotlin
jubakoAdapter.reload(SOME_UNIQUE_ID)
```

You can also reload with some arbitrary data (*payload*) that will be passed onto the
`onReload` function of `ContentDescription` (see `onReload` in next section)

```kotlin
jubakoAdapter.reload(SOME_UNIQUE_ID, "Hello, World!")
```

#### Handling a reload
Although you can call `reload` on the `JubakoAdapter` or `JubakoViewHolder`, you still
need to handle what happens when its called. You must implement the function
`ContentDescription::onReload` which in simplest case reassigns `ContentDescription::data`
with a new `LiveData<T>` as follows:-

```kotlin
ContentDescription(
    id = SOME_UNIQUE_ID,
    viewHolderFactory = TestViewHolderFactory(),
    data = object : LiveData<String>() {
        override fun onActive() {
            thread {
                postValue("Initial value")
            }.run()
        }
    },
    onReload = { contentDescription, data ->
        contentDescription.data = object : LiveData<String>() {
            override fun onActive() {
                thread {
                    postValue("Peek-a-Boo! $data")
                }.run()
            }
        }
    })
```

The example shows that `onReload` provides a function that reassigns `data`,
`JubakoAdapter` will effectively call this before it observes `data` again.

## JubakoViewHolder events
It is possible to propagate events from a `JubakoViewHolder` to `JubakoAdapter`
where integrations can listen for events by providing a callback function
to `JubakoAdapter::onViewHolderEvent`

First we need to fire an event from the `JubakoViewHolder`, eg:-

```kotlin
class ExampleViewHolder(view: View) : JubakoViewHolder<String>(view) {
    init {
        itemView.findViewById(R.id.hello_button).apply {
            setOnClickListener {
                postClickEvent(R.id.hello_button)
            }
        }
    }
}
```
Then later we hook into `JubakoAdapter` and respond to the event:-

```kotlin
adapter.onViewHolderEvent = {
    when (it) {
        is JubakoViewHolder.Event.Click -> {
            when(it.viewId) {
                R.id.hello_button -> showMessage("Hello!")
            }
        }
    }
}
```

## Resetting
In order to maintain state across configuration changes making another call to `content.load(JubakoAssembler)` will do nothing 
unless you call `jubako.reset()` beforehand.
