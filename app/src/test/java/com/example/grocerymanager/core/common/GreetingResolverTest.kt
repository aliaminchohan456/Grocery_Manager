package com.example.grocerymanager.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GreetingResolverTest {
    @Test fun `before 5 returns Hello`() {
        assertThat(GreetingResolver.forHour(0)).isEqualTo(Greeting.Hello)
        assertThat(GreetingResolver.forHour(4)).isEqualTo(Greeting.Hello)
    }

    @Test fun `morning bucket 5 to 11`() {
        assertThat(GreetingResolver.forHour(5)).isEqualTo(Greeting.Morning)
        assertThat(GreetingResolver.forHour(11)).isEqualTo(Greeting.Morning)
    }

    @Test fun `afternoon bucket 12 to 16`() {
        assertThat(GreetingResolver.forHour(12)).isEqualTo(Greeting.Afternoon)
        assertThat(GreetingResolver.forHour(16)).isEqualTo(Greeting.Afternoon)
    }

    @Test fun `evening bucket 17 to 21`() {
        assertThat(GreetingResolver.forHour(17)).isEqualTo(Greeting.Evening)
        assertThat(GreetingResolver.forHour(21)).isEqualTo(Greeting.Evening)
    }

    @Test fun `night bucket 22 to 23 returns Hello`() {
        assertThat(GreetingResolver.forHour(22)).isEqualTo(Greeting.Hello)
        assertThat(GreetingResolver.forHour(23)).isEqualTo(Greeting.Hello)
    }
}
