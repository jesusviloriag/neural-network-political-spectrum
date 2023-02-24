package com.guaire.aitests.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 * A Message.
 */
@Entity
@Table(name = "message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "manual_bias")
    private String manualBias;

    @Column(name = "a_i_bias")
    private String aIBias;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Message id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public Message text(String text) {
        this.setText(text);
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getManualBias() {
        return this.manualBias;
    }

    public Message manualBias(String manualBias) {
        this.setManualBias(manualBias);
        return this;
    }

    public void setManualBias(String manualBias) {
        this.manualBias = manualBias;
    }

    public String getaIBias() {
        return this.aIBias;
    }

    public Message aIBias(String aIBias) {
        this.setaIBias(aIBias);
        return this;
    }

    public void setaIBias(String aIBias) {
        this.aIBias = aIBias;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        return id != null && id.equals(((Message) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Message{" +
            "id=" + getId() +
            ", text='" + getText() + "'" +
            ", manualBias='" + getManualBias() + "'" +
            ", aIBias='" + getaIBias() + "'" +
            "}";
    }
}
