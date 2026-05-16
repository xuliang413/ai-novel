import { postRequest } from '/@/lib/axios';

export const novelWritingCalendarApi = {
  checkin: (param) => {
    return postRequest('/novel/write/calendar/checkin', param);
  },

  query: (param) => {
    return postRequest('/novel/write/calendar/query', param);
  },

  streak: (param) => {
    return postRequest('/novel/write/calendar/streak', param);
  },
};
