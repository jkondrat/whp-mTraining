package org.motechproject.whp.mtraining.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;

import java.util.List;
import java.util.UUID;

/**
 * Couch document object representing a Question.
 * Message
 *   + name    : Question name
 *   + description : Question description
 *   + contentId   : UUID that for a question (different from the _id generated by couch)
 *   + version     : question version (a quiz can have multiple versions, different versions of the question will have same contentId)
 *   + externalContentId  : Id that points to an external file or resource that is associated with the question.For eg. an audio file that is played to the enrollee
 *   + createdBy    : Author of the question
 *   + createdOn    : Date on which question was created
 *   + answer       : correct option for the question
 *   +options       : options to be played out for this question
 */
@TypeDiscriminator("doc.type === 'Question'")
public class Question extends CourseContent {
    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private List<String> options;

    public Question(boolean isActive, String name, String description, String externalId,List<String> options, String createdBy) {
        super(name, UUID.fromString(externalId), null, createdBy, null, isActive);
        this.name = name;
        this.description = description;
        this.options = options;
    }

    public Question(UUID contentId, Integer version, boolean isActive, String name, String description, String externalId, List<String> options, String createdBy) {
        super(name, UUID.fromString(externalId), version, createdBy, null, isActive);
        this.name = name;
        this.description = description;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getOptions() {
        return options;
    }

    public Boolean isCorrectAnswer(String selectedOption) {
        return true;
    }
}
