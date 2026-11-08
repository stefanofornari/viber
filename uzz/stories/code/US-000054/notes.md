# US-000054 Implementation Notes

The GUI actor selector now includes Coding. Selecting it creates a `CodingActor` with the configured streaming LLM and project `FileSystemTools`, then displays a distinct Coding Phase indicator.

If initialization fails, the controller reports the failure in the phase label and restores the previous actor selection without switching the actor or conversation view.
