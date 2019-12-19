package com.justeat.jubako.extensions

import com.justeat.jubako.PaginatedContentLoadingStrategy

fun pageSize(pageSize: Int) = PaginatedContentLoadingStrategy(pageSize)
