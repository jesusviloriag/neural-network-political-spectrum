import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Word from './word';
import WordDetail from './word-detail';
import WordUpdate from './word-update';
import WordDeleteDialog from './word-delete-dialog';

const WordRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Word />} />
    <Route path="new" element={<WordUpdate />} />
    <Route path=":id">
      <Route index element={<WordDetail />} />
      <Route path="edit" element={<WordUpdate />} />
      <Route path="delete" element={<WordDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default WordRoutes;
