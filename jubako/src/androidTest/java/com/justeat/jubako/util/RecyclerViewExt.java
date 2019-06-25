package com.justeat.jubako.util;


import android.content.res.Resources;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.contrib.RecyclerViewActions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class RecyclerViewExt {

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    public static class RecyclerViewMatcher {
        private final int recyclerViewId;

        public RecyclerViewMatcher(int recyclerViewId) {
            this.recyclerViewId = recyclerViewId;
        }

        public Matcher<View> atPosition(final int position) {
            return atPositionOnView(position, -1);
        }

        public Matcher<View> atPositionOnView(final int position, final int targetViewId) {

            return new TypeSafeMatcher<View>() {
                Resources resources = null;
                View childView;

                public void describeTo(Description description) {
                    String idDescription = Integer.toString(recyclerViewId);
                    if (this.resources != null) {
                        try {
                            idDescription = this.resources.getResourceName(recyclerViewId);
                        } catch (Resources.NotFoundException var4) {
                            idDescription = String.format("%s (resource name not found)", recyclerViewId);
                        }
                    }
                    description.appendText("with id: " + idDescription);
                }

                public boolean matchesSafely(View view) {
                    this.resources = view.getResources();

                    if (childView == null) {
                        RecyclerView recyclerView =
                                (RecyclerView) view.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null && recyclerView.getId() == recyclerViewId) {
                            childView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                        } else {
                            return false;
                        }
                    }

                    if (targetViewId == -1) {
                        return view == childView;
                    } else {
                        View targetView = childView.findViewById(targetViewId);
                        return view == targetView;
                    }

                }
            };
        }
    }

    public static class ItemCountAssertion implements ViewAssertion {
        private final int mExpectedCount;

        public ItemCountAssertion(int expectedCount) {
            mExpectedCount = expectedCount;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }

            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(mExpectedCount));
        }
    }

    public static class ItemOrderAssertion implements ViewAssertion {
        private final Matcher<View> mAnchor;
        private final Matcher<View>[] mFollowing;

        @SafeVarargs
        private ItemOrderAssertion(Matcher<View> anchor, Matcher<View>... following) {
            mAnchor = anchor;
            mFollowing = following;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            RecyclerView recyclerView = (RecyclerView) view;

            int anchorPosition = getPosition(recyclerView, mAnchor);

            for (int i = 0; i < mFollowing.length; i++) {
                int position = getPosition(recyclerView, mFollowing[i]);
                assertThat(position - anchorPosition, is(i + 1));
            }
        }
    }

    private static class NoMatchingItemAssertion implements ViewAssertion {
        private final Matcher<View> mItemMatcher;

        public NoMatchingItemAssertion(Matcher<View> itemMatcher) {
            mItemMatcher = itemMatcher;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (!(view instanceof RecyclerView)) {
                throw new AssertionError("MatchingItemAssertion: view is not the RecyclerView");
            }
            RecyclerView recyclerView = (RecyclerView) view;
            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            for (int position = 0; position < layoutManager.getItemCount(); ++position) {
                if (mItemMatcher.matches(layoutManager.findViewByPosition(position))) {
                    throw new AssertionError("MatchingItemAssertion: no matching views expected but a matching view was found");
                }
            }
        }
    }

    /**
     * This method helps to find matching view position in a RecyclerView.
     * Unfortunately, required methods from {@link RecyclerViewActions} are marked as privates,
     * so to prevent copying whole methods I just used reflection which is fine as it's not a production
     * code and eventually the main goal of tests is to emphasise changes in product.
     *
     * @return position if matcher was precise enough, throws exception otherwise or if reflection wasn't able to resolve methods or field
     */
    @SuppressWarnings("unchecked")
    private static <VH extends ViewHolder> int getPosition(RecyclerView recyclerView, Matcher<View> matcher) {
        try {
            Method viewHolderMatcherFunc = RecyclerViewActions.class.getDeclaredMethod("viewHolderMatcher", Matcher.class);
            viewHolderMatcherFunc.setAccessible(true);
            Matcher<VH> viewHolderMatcher = (Matcher<VH>) viewHolderMatcherFunc.invoke(null, matcher);

            Method itemsMatchingFunc = RecyclerViewActions.class.getDeclaredMethod("itemsMatching", RecyclerView.class, Matcher.class, int.class);
            itemsMatchingFunc.setAccessible(true);
            List matches = (List) itemsMatchingFunc.invoke(null, recyclerView, viewHolderMatcher, 1);

            if (matches != null && matches.size() == 1) {
                Object match = matches.get(0);
                Field positionField = match.getClass().getDeclaredField("position");
                positionField.setAccessible(true);
                return positionField.getInt(match);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Reflection can't resolve method", e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Reflection can't resolve field", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Calling method via reflection throws exception", e);
        }  catch (IllegalAccessException e) {
            throw new RuntimeException("Calling method via reflection throws exception", e);
        }
        throw new NoMatchingViewException.Builder()
                .includeViewHierarchy(true)
                .withRootView(recyclerView)
                .withViewMatcher(matcher)
                .build();
    }
}
