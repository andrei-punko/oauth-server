package by.andd3dfx.auth.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@SessionAttributes("authorizationRequest")
public class MainController {

    private Map<Object, String> nodeMap = new ConcurrentHashMap<>();

    public void setNode(String node) {
        if (node == null) {
            node = "";
        }

        SecurityContext context = SecurityContextHolder.getContext();
        nodeMap.put(((WebAuthenticationDetails)context.getAuthentication().getDetails()).getRemoteAddress(), node);
    }

    private String getNode() {
        SecurityContext context = SecurityContextHolder.getContext();
        String node = nodeMap.get(((WebAuthenticationDetails)context.getAuthentication().getDetails()).getRemoteAddress());
        return (node != null) ? node.toLowerCase() : "";
    }

    @Value("${auth.profile}")
    private String authProfile;

    @GetMapping("/login")
    public String login(@RequestParam Map<String,String> allParams, Model model) {
        if (allParams.containsKey("error")) {
            model.addAttribute("loginError", true);
        }
        return getPrefix(model) + "login";
    }

    @RequestMapping("/oauth/custom_confirm_access")
    public String custom_confirm_access(Model model) {
        return getPrefix(model) + "authorize";
    }

    private String getPrefix(Model model) {
        model.addAttribute("nodeName", getNode());
        if (authProfile.equals("mvpd")) {
            return "mvpd-";
        }
        if (authProfile.equals("ott")) {
            return "ott-";
        }
        return "";
    }
}