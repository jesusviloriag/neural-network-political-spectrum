export interface IWord {
  id?: number;
  name?: string;
  value?: number | null;
}

export const defaultValue: Readonly<IWord> = {};
