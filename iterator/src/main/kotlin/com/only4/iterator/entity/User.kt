package com.only4.iterator.entity

class User(
    var id: Int = 0,
    var name: String,
    var age: Int
) : Iterable<String> {
    override fun iterator(): Iterator<String> {
        return object : Iterator<String> {
            private var count = 3

            override fun next(): String {
                count--
                when(count) {
                    0 -> return id.toString()
                    1 -> return name
                    2 -> return age.toString()
                }
                throw NoSuchElementException()
            }

            override fun hasNext(): Boolean {
                return count > 0
            }
        }
    }

    override fun toString(): String {
        return "User(id=$id, name='$name', age=$age)"
    }
}
