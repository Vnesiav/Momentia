package com.example.momentia.DTO

sealed class MemorySection {
    data class Header(val memory: Memory) : MemorySection()
    data class Item(val memory: Memory) : MemorySection()
}
