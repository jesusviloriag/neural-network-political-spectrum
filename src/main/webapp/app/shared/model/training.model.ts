import dayjs from 'dayjs';

export interface ITraining {
  id?: number;
  twitterFeedFileContentType?: string;
  twitterFeedFile?: string;
  aiFileContentType?: string | null;
  aiFile?: string | null;
  timeStamp?: string | null;
  status?: string | null;
  isLeft?: boolean | null;
}

export const defaultValue: Readonly<ITraining> = {
  isLeft: false,
};
