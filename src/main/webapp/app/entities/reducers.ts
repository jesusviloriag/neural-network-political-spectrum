import message from 'app/entities/message/message.reducer';
import word from 'app/entities/word/word.reducer';
import training from 'app/entities/training/training.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  message,
  word,
  training,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
