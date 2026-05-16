import { postRequest } from '/@/lib/axios';

export const novelUserApiKeyApi = {
  save: (param) => {
    return postRequest('/novel/user-api-key/save', param);
  },

  get: () => {
    return postRequest('/novel/user-api-key/get');
  },

  test: (param) => {
    return postRequest('/novel/user-api-key/test', param);
  },

  usage: () => {
    return postRequest('/novel/user-api-key/usage');
  },
};
