/*
 * Copyright 2026 the original author or authors from the Viber project
 * (https://stefanofornari.github.io/viber).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ste.ai.viber.tools;

import static ste.uzzo.agent.tools.AskTools.ASK_CONFIRMATION;
import static ste.uzzo.agent.tools.AskTools.ASK_MULTIPLE_CHOICE;
import static ste.uzzo.agent.tools.AskTools.ASK_QUESTION;
import static ste.uzzo.agent.tools.AskTools.ASK_SINGLE_CHOICE;

/**
 *
 */
public enum QuestionType {
    OPEN_ENDED(ASK_QUESTION),
    SINGLE_CHOICE(ASK_SINGLE_CHOICE),
    MULTIPLE_CHOICE(ASK_MULTIPLE_CHOICE),
    CONFIRMATION(ASK_CONFIRMATION);

    public final String name;
    QuestionType(String name) { this.name = name; }

    public static QuestionType from(final String text) {
        for (QuestionType type : QuestionType.values()) {
            if (type.name.equals(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text);
    }
}
