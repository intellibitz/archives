import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException

@WebServlet(name = "helloworld", value = [""])
@Suppress("serial")
class HelloServlet : HttpServlet() {

    @Throws(IOException::class)
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val out = resp.writer
        out.println("Hello, world - App Engine Flexible")
    }
}
// [END gae_flex_servlet]
