import { postRequest } from '/@/lib/axios';

const assetTypes = ['character', 'location', 'clue', 'volume', 'item', 'event', 'cheat', 'alias', 'rule'];

function endpoint(assetType, action) {
  return `/novel/${assetType}/${action}`;
}

export const novelAssetApi = assetTypes.reduce((api, assetType) => {
  api[assetType] = {
    add: (param) => postRequest(endpoint(assetType, 'add'), param),
    query: (param) => postRequest(endpoint(assetType, 'query'), param),
    detail: (param) => postRequest(endpoint(assetType, 'detail'), param),
    update: (param) => postRequest(endpoint(assetType, 'update'), param),
    archive: (param) => postRequest(endpoint(assetType, 'archive'), param),
  };
  return api;
}, {});

