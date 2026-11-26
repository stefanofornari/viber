# Notes for US-000065

- `UserStoryEditor` is an `fx:root` VBox loaded from `UserStoryEditor.fxml`, so it can also be declared directly in other FXML (the dev mode window does).
- Fields are bound bidirectionally to the story's properties and unbound when the story is replaced, so a replaced story is never modified by the UI.
- No styling code: the control relies on the host's AtlantaFX theme (`text-bold` label style class).
