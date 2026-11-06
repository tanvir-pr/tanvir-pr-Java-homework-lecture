package hu.neumann.homework.web;

import hu.neumann.homework.testmenu.CalculatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestMenuController.class)
@Import(CalculatorService.class)
class TestMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getShowsForm() throws Exception {
        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(view().name("test"))
                .andExpect(model().attributeExists("a", "b", "operation"));
    }

    @Test
    void postAddShowsResult() throws Exception {
        mockMvc.perform(post("/test")
                        .param("a", "8")
                        .param("b", "2")
                        .param("operation", "add"))
                .andExpect(status().isOk())
                .andExpect(view().name("test"))
                .andExpect(model().attribute("result", 10.0));
    }

    @Test
    void postDivideByZeroShowsError() throws Exception {
        mockMvc.perform(post("/test")
                        .param("a", "8")
                        .param("b", "0")
                        .param("operation", "divide"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(content().string(containsString("Division by zero")));
    }

    @Test
    void postMultiplyShowsResult() throws Exception {
        mockMvc.perform(post("/test")
                        .param("a", "3")
                        .param("b", "4")
                        .param("operation", "multiply"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("result", 12.0));
    }
}
