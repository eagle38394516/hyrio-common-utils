package xyz.hyrio.common.components;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.hyrio.common.exception.request.NotFoundException;

@RestController
@ConditionalOnExpression("!${swagger.enabled:false}")
public class CommonController {
    @RequestMapping("/**")
    public void index() {
        throw new NotFoundException("API not found");
    }
}
