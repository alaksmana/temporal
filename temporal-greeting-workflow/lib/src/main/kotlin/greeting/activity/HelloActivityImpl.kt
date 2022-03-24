package greeting.activity

class HelloActivityImpl: HelloActivity {

    override fun hello(name: String): String {
        return "hello, $name"
    }
}