import { postRequest } from '/@/lib/axios';

export const novelGraphApi = {
  health: (param) => {
    return postRequest('/novel/graph/health', param);
  },

  characterRelation: (param) => {
    return postRequest('/novel/graph/character-relation', param);
  },

  clueAdvancement: (param) => {
    return postRequest('/novel/graph/clue-advancement', param);
  },

  locationCharacter: (param) => {
    return postRequest('/novel/graph/location-character', param);
  },

  itemFlow: (param) => {
    return postRequest('/novel/graph/item-flow', param);
  },
};
