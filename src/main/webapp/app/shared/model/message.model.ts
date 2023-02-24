export interface IMessage {
  id?: number;
  text?: string;
  manualBias?: string | null;
  aIBias?: string | null;
}

export const defaultValue: Readonly<IMessage> = {};
