package com.example.weatherapp

import kotlin.random.Random

fun main(args: Array<String>) {
    val a: Array<Int> = Array(10, {0})
    for(i in 0 .. a.size - 1) {
        a[i] = Random.nextInt(0, 10)
        print("${a[i]}, ")
    }
    searchMin(a)
}

fun searchMin(a: Array<Int>) {
    var smallest = a[0]
    var smallest_index = 0
    var i = 0
    while(a[i] != a.size - 1) {
        if (a[i] < smallest) {
            smallest = a[i]
            smallest_index = i
        }
        i++
    }
    println("Smallest = ${smallest}, Smallest Index = ${smallest_index}")
}