package it.vitalegi.translator.resource;

import it.vitalegi.translator.App;
import it.vitalegi.translator.model.AppStats;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("uptime")
@AllArgsConstructor
public class UptimeResource {

    @GetMapping
    public AppStats uptime() {
        return App.STATS;
    }
}
