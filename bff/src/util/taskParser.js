export const parseStreamFields = (fields) => {
  const data = {};

  for (let i = 0; i < fields.length; i += 2) {
    data[fields[i]] = fields[i + 1];
  }

  return {
    _id: data._id,
    status: data.status,
    ...data,
  };
};
