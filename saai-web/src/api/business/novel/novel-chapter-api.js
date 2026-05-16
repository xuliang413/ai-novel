import { postRequest } from '/@/lib/axios';

export const novelChapterApi = {
  query: (param) => {
    return postRequest('/novel/chapter/query', param);
  },

  detail: (param) => {
    return postRequest('/novel/chapter/detail', param);
  },

  update: (param) => {
    return postRequest('/novel/chapter/update', param);
  },

  archive: (param) => {
    return postRequest('/novel/chapter/archive', param);
  },
};
