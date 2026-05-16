import { postRequest } from '/@/lib/axios';

export const novelProjectApi = {
  add: (param) => {
    return postRequest('/novel/project/add', param);
  },

  query: (param) => {
    return postRequest('/novel/project/query', param);
  },

  detail: (param) => {
    return postRequest('/novel/project/detail', param);
  },

  update: (param) => {
    return postRequest('/novel/project/update', param);
  },

  archive: (param) => {
    return postRequest('/novel/project/archive', param);
  },
};
