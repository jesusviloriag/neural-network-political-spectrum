import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { openFile, byteSize, Translate, TextFormat, getSortState, JhiPagination, JhiItemCount } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ITraining } from 'app/shared/model/training.model';
import { getEntities, doTraining } from './training.reducer';

export const Training = () => {
  const dispatch = useAppDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getSortState(location, ITEMS_PER_PAGE, 'id'), location.search)
  );

  const trainingList = useAppSelector(state => state.training.entities);
  const loading = useAppSelector(state => state.training.loading);
  const totalItems = useAppSelector(state => state.training.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      })
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [location.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const handleTraining = () => {
      dispatch(
          doTraining()
        );
    };

  return (
    <div>
      <h2 id="training-heading" data-cy="TrainingHeading">
        Trainings <br/><br/>
        <h5>Steps:</h5>
        <h5>1. Create two pre-trainings (one for left and one for right)</h5>
        <h5>2. Train AI</h5>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/training/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new pre-training
          </Link>
          &nbsp;
          <Button className="me-2" color="success" onClick={handleTraining} disabled={loading}>
            <FontAwesomeIcon icon="gears" spin={loading} /> Train
          </Button>
        </div>
      </h2>
      <div className="table-responsive">
        {trainingList && trainingList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  ID <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('twitterFeedFile')}>
                  Twitter Feed File <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('aiFile')}>
                  Ai File <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('timeStamp')}>
                  Time Stamp <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('status')}>
                  Status <FontAwesomeIcon icon="sort" />
                </th>
                <th className="hand" onClick={sort('isLeft')}>
                  Is Left <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {trainingList.map((training, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/training/${training.id}`} color="link" size="sm">
                      {training.id}
                    </Button>
                  </td>
                  <td>
                    {training.twitterFeedFile ? (
                      <div>
                        {training.twitterFeedFileContentType ? (
                          <a onClick={openFile(training.twitterFeedFileContentType, training.twitterFeedFile)}>Open &nbsp;</a>
                        ) : null}
                        <span>
                          {training.twitterFeedFileContentType}, {byteSize(training.twitterFeedFile)}
                        </span>
                      </div>
                    ) : null}
                  </td>
                  <td>
                    {training.aiFile ? (
                      <div>
                        {training.aiFileContentType ? (
                          <a onClick={openFile(training.aiFileContentType, training.aiFile)}>Open &nbsp;</a>
                        ) : null}
                        <span>
                          {training.aiFileContentType}, {byteSize(training.aiFile)}
                        </span>
                      </div>
                    ) : null}
                  </td>
                  <td>{training.timeStamp ? <TextFormat type="date" value={training.timeStamp} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{training.status}</td>
                  <td>{training.isLeft ? 'true' : 'false'}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/training/${training.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Trainings found</div>
        )}
      </div>
      {totalItems ? (
        <div className={trainingList && trainingList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Training;
