import { postRequest } from '/@/lib/axios';

export const novelDashboardApi = {
  assetSummary: (param) => {
    return postRequest('/novel/dashboard/asset-summary', param);
  },

  chapterProgress: (param) => {
    return postRequest('/novel/dashboard/chapter-progress', param);
  },

  recentLogs: (param) => {
    return postRequest('/novel/dashboard/recent-logs', param);
  },

  recentPatches: (param) => {
    return postRequest('/novel/dashboard/recent-patches', param);
  },

  pendingSessions: (param) => {
    return postRequest('/novel/dashboard/pending-sessions', param);
  },

  totalWords: (param) => {
    return postRequest('/novel/dashboard/total-words', param);
  },
};
