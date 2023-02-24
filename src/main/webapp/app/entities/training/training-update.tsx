import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, ValidatedField, ValidatedForm, ValidatedBlobField } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ITraining } from 'app/shared/model/training.model';
import { getEntity, updateEntity, createEntity, reset } from './training.reducer';

export const TrainingUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const trainingEntity = useAppSelector(state => state.training.entity);
  const loading = useAppSelector(state => state.training.loading);
  const updating = useAppSelector(state => state.training.updating);
  const updateSuccess = useAppSelector(state => state.training.updateSuccess);

  const handleClose = () => {
    navigate('/training' + location.search);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    values.timeStamp = convertDateTimeToServer(values.timeStamp);

    const entity = {
      ...trainingEntity,
      ...values,
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          timeStamp: displayDefaultDateTime(),
        }
      : {
          ...trainingEntity,
          timeStamp: convertDateTimeFromServer(trainingEntity.timeStamp),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="aitestsApp.training.home.createOrEditLabel" data-cy="TrainingCreateUpdateHeading">
            Create or edit a Training<br/><br/>
                                             <h5>1. Tweets file: one tweet per line</h5>
                                             <h5>2. Words file: one word per line</h5><br/>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? <ValidatedField name="id" required readOnly id="training-id" label="ID" validate={{ required: true }} /> : null}
              <ValidatedBlobField
                label="Twitter Feed File"
                id="training-twitterFeedFile"
                name="twitterFeedFile"
                data-cy="twitterFeedFile"
                openActionLabel="Open"
                validate={{
                  required: { value: true, message: 'This field is required.' },
                }}
              />
              <ValidatedBlobField label="Words File" id="training-aiFile" name="aiFile" data-cy="aiFile" openActionLabel="Open" />
              <ValidatedField
                label="Time Stamp"
                id="training-timeStamp"
                name="timeStamp"
                data-cy="timeStamp"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField label="Status" id="training-status" name="status" data-cy="status" type="text" />
              <ValidatedField label="Is Left" id="training-isLeft" name="isLeft" data-cy="isLeft" check type="checkbox" />
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/training" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default TrainingUpdate;
