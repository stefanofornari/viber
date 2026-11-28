this is an uzzo driven project; we recently added a progress indicator functinality; we want now to add the following feature:
- while a prompt is being processed, we shall be able to interrupt the conversation
- pressing ESC on the chat or input window shall open a confirmation notification dialog 
  - modal but without window decorations
  - compact layout
  - sentence that tells to press ENTER to stop the current chat
  - two buttons
    - "never mind" -> close the dialog, don't do anything
    - "stop conversation" -> close the dialog, stop the conversation of the selected chat
- stop the conversation means that the interaction with the LLM shall stop
  - no more prompts shall be sent once obtained an answer
  - possibly the streaming being receved shall be stopped (in a lanchain4j graceful manner)
  - any question/answer backand forth shall be interrupted (maybe this is given by the above)
- the progress indicator shall disappear as well
let's scope the feature with particular attention to the threading aspects and making sure we heck with the langchain4j documentation (https://docs.langchain4j.dev/)