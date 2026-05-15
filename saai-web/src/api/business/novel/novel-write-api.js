import { postRequest } from '/@/lib/axios';

export const novelWriteApi = {
  start: (param) => {
    return postRequest('/novel/write/start', param);
  },

  passContentReview: (param) => {
    return postRequest('/novel/write/content/pass', param);
  },

  confirmPatch: (param) => {
    return postRequest('/novel/write/patch/confirm', param);
  },

  backToContentReview: (param) => {
    return postRequest('/novel/write/patch/back', param);
  },

  recover: (param) => {
    return postRequest('/novel/write/recover', param);
  },

  undo: (param) => {
    return postRequest('/novel/write/undo', param);
  },
};
