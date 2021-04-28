const tableData = JSON.parse(
    `[{
  "id": 0,
  "key": 0,
  "title": "PSK is cool",
  "uploadTime": "2021-04-25 11:11",
  "size": "25 MB"
}, {
  "id": 1,
  "key": 1,
  "title": "GMM is cool",
  "uploadTime": "2021-04-20 09:22",
  "size": "1000 MB"
}, {
  "id": 2,
  "key": 2,
  "title": "Life is cool",
  "uploadTime": "2021-04-19 16:01",
  "size": "3 MB"
}]`,
);
const sortOption = {};
class fakeData {
    constructor(size) {
        this.size = size || 2000;
        this.datas = [];
        this.sortKey = null;
        this.sortDir = null;
    }
    dataModel(index) {
        return tableData[index];
    }
    getObjectAt(index) {
        if (index < 0 || index > this.size) {
            return undefined;
        }
        if (this.datas[index] === undefined) {
            this.datas[index] = this.dataModel(index);
        }
        return this.datas[index];
    }
    getAll() {
        if (this.datas.length < this.size) {
            for (let i = 0; i < this.size; i++) {
                this.getObjectAt(i);
            }
        }
        return this.datas.slice();
    }

    getSize() {
        return this.size;
    }
    getSortAsc(sortKey) {
        sortOption.sortKey = sortKey;
        sortOption.sortDir = 'ASC';
        return this.datas.sort(this.sort);
    }
    getSortDesc(sortKey) {
        sortOption.sortKey = sortKey;
        sortOption.sortDir = 'DESC';
        return this.datas.sort(this.sort);
    }
    sort(optionA, optionB) {
        const valueA = optionA[sortOption.sortKey].toUpperCase();
        const valueB = optionB[sortOption.sortKey].toUpperCase();
        let sortVal = 0;
        if (valueA > valueB) {
            sortVal = 1;
        }
        if (valueA < valueB) {
            sortVal = -1;
        }
        if (sortVal !== 0 && sortOption.sortDir === 'DESC') {
            return sortVal * (-1);
        }
        return sortVal;
    }
}
export default fakeData;
