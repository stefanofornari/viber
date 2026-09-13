package ste.ai.toolify.settings;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.stage.Stage;
import atlantafx.base.theme.NordLight;
import ste.ai.toolify.net.TrustAllCertificates;

public class PromptsPanelDemo extends Application {

    @Override
    public void start(Stage stage) {
        try {
            TrustAllCertificates.disableCertificateValidation();
            Application.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());

            Map<String, String> prompts = new HashMap<>();
            prompts.put("Greeting", "Hello, how can I help you today?");
            prompts.put("Code Review", "Please review the following code for security vulnerabilities and performance issues.");

            PromptsPanelController controller = new PromptsPanelController(prompts);

            PreferencesFx preferencesFx = PreferencesFx.of(PromptsPanelDemo.class,
                Category.of("Prompts",
                    Group.of("Manage Your Prompts",
                        Setting.of(controller.getView())
                    )
                )
            ).persistWindowState(false);

            preferencesFx.show(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
