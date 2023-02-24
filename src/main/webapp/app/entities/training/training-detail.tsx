import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { openFile, byteSize, TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './training.reducer';

export const TrainingDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const trainingEntity = useAppSelector(state => state.training.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="trainingDetailsHeading">Training</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{trainingEntity.id}</dd>
          <dt>
            <span id="twitterFeedFile">Twitter Feed File</span>
          </dt>
          <dd>
            {trainingEntity.twitterFeedFile ? (
              <div>
                {trainingEntity.twitterFeedFileContentType ? (
                  <a onClick={openFile(trainingEntity.twitterFeedFileContentType, trainingEntity.twitterFeedFile)}>Open&nbsp;</a>
                ) : null}
                <span>
                  {trainingEntity.twitterFeedFileContentType}, {byteSize(trainingEntity.twitterFeedFile)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="aiFile">Ai File</span>
          </dt>
          <dd>
            {trainingEntity.aiFile ? (
              <div>
                {trainingEntity.aiFileContentType ? (
                  <a onClick={openFile(trainingEntity.aiFileContentType, trainingEntity.aiFile)}>Open&nbsp;</a>
                ) : null}
                <span>
                  {trainingEntity.aiFileContentType}, {byteSize(trainingEntity.aiFile)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="timeStamp">Time Stamp</span>
          </dt>
          <dd>{trainingEntity.timeStamp ? <TextFormat value={trainingEntity.timeStamp} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="status">Status</span>
          </dt>
          <dd>{trainingEntity.status}</dd>
          <dt>
            <span id="isLeft">Is Left</span>
          </dt>
          <dd>{trainingEntity.isLeft ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/training" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/training/${trainingEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default TrainingDetail;
