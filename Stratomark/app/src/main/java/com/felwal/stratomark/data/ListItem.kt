package com.felwal.stratomark.data

abstract class ListItem {

    abstract fun sameItemAs(item: ListItem)

    abstract fun sameContentAs(item: ListItem)
}