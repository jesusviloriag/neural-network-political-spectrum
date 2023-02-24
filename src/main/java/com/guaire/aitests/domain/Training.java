package com.guaire.aitests.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Training.
 */
@Entity
@Table(name = "training")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Training implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "twitter_feed_file", nullable = false)
    private byte[] twitterFeedFile;

    @NotNull
    @Column(name = "twitter_feed_file_content_type", nullable = false)
    private String twitterFeedFileContentType;

    @Lob
    @Column(name = "ai_file")
    private byte[] aiFile;

    @Column(name = "ai_file_content_type")
    private String aiFileContentType;

    @Column(name = "time_stamp")
    private ZonedDateTime timeStamp;

    @Column(name = "status")
    private String status;

    @Column(name = "is_left")
    private Boolean isLeft;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Training id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getTwitterFeedFile() {
        return this.twitterFeedFile;
    }

    public Training twitterFeedFile(byte[] twitterFeedFile) {
        this.setTwitterFeedFile(twitterFeedFile);
        return this;
    }

    public void setTwitterFeedFile(byte[] twitterFeedFile) {
        this.twitterFeedFile = twitterFeedFile;
    }

    public String getTwitterFeedFileContentType() {
        return this.twitterFeedFileContentType;
    }

    public Training twitterFeedFileContentType(String twitterFeedFileContentType) {
        this.twitterFeedFileContentType = twitterFeedFileContentType;
        return this;
    }

    public void setTwitterFeedFileContentType(String twitterFeedFileContentType) {
        this.twitterFeedFileContentType = twitterFeedFileContentType;
    }

    public byte[] getAiFile() {
        return this.aiFile;
    }

    public Training aiFile(byte[] aiFile) {
        this.setAiFile(aiFile);
        return this;
    }

    public void setAiFile(byte[] aiFile) {
        this.aiFile = aiFile;
    }

    public String getAiFileContentType() {
        return this.aiFileContentType;
    }

    public Training aiFileContentType(String aiFileContentType) {
        this.aiFileContentType = aiFileContentType;
        return this;
    }

    public void setAiFileContentType(String aiFileContentType) {
        this.aiFileContentType = aiFileContentType;
    }

    public ZonedDateTime getTimeStamp() {
        return this.timeStamp;
    }

    public Training timeStamp(ZonedDateTime timeStamp) {
        this.setTimeStamp(timeStamp);
        return this;
    }

    public void setTimeStamp(ZonedDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return this.status;
    }

    public Training status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsLeft() {
        return this.isLeft;
    }

    public Training isLeft(Boolean isLeft) {
        this.setIsLeft(isLeft);
        return this;
    }

    public void setIsLeft(Boolean isLeft) {
        this.isLeft = isLeft;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Training)) {
            return false;
        }
        return id != null && id.equals(((Training) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Training{" +
            "id=" + getId() +
            ", twitterFeedFile='" + getTwitterFeedFile() + "'" +
            ", twitterFeedFileContentType='" + getTwitterFeedFileContentType() + "'" +
            ", aiFile='" + getAiFile() + "'" +
            ", aiFileContentType='" + getAiFileContentType() + "'" +
            ", timeStamp='" + getTimeStamp() + "'" +
            ", status='" + getStatus() + "'" +
            ", isLeft='" + getIsLeft() + "'" +
            "}";
    }
}
