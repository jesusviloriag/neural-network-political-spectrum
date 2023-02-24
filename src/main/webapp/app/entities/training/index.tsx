import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Training from './training';
import TrainingDetail from './training-detail';
import TrainingUpdate from './training-update';
import TrainingDeleteDialog from './training-delete-dialog';

const TrainingRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Training />} />
    <Route path="new" element={<TrainingUpdate />} />
    <Route path=":id">
      <Route index element={<TrainingDetail />} />
      <Route path="edit" element={<TrainingUpdate />} />
      <Route path="delete" element={<TrainingDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TrainingRoutes;
