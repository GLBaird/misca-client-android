package org.qumodo.data.models;

import java.util.List;

public class MiscaWorkflowQuestion extends MiscaWorkflowStep {

    public static class Question {
        public String label, id;

        public Question(String label, String id) {
            this.label = label;
            this.id = id;
        }
    }

    private String message;
    private Question[] questions;

    public MiscaWorkflowQuestion(String id, String message, Question[] questions) {
        this.type = MiscaWorkflowType.QUESTION;
        this.id = id;
        this.message = message;
        this.questions = questions;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Question[] getQuestions() {
        return questions;
    }

}
