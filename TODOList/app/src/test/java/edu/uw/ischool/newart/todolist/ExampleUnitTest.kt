package edu.uw.ischool.newart.todolist

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun todo_produces_correct_String() {
        // Create a TODO with a due date of 1/1/2014 11:59am
        //val t = Todo("Eat lunch", Date)
        // Make sure the TODO toString matches
        //assertEquals("Eat lunch (Due )", t.toString())
    }
}