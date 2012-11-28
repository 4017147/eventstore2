package no.ks.eventstore2.eventstore.formProcessor;

import no.ks.eventstore2.Event;

public class FormReceived extends Event {
    private String formId;

    public FormReceived(String formId) {
        this.formId = formId;
        this.aggregateId = "FORM";
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }
}
