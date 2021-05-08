import clone from 'clone';
import { DateCell, ImageCell, LinkCell, TextCell } from '../../commonHelpers/helperCells';

const renderCell = (object, type, key) => {
    const value = object[key];
    switch (type) {
        case 'ImageCell':
            return ImageCell(value);
        case 'DateCell':
            return DateCell(value);
        case 'LinkCell':
            return LinkCell(value);
        default:
            return TextCell(value);
    }
};

const columns = [
    {
        title: 'Title',
        key: 'title',
        width: 100,
        render: object => renderCell(object, 'TextCell', 'title')
    },
    {
        title: 'Upload time',
        key: 'uploadTime',
        width: 100,
        render: object => renderCell(object, 'TextCell', 'uploadTime')
    },
    {
        title: 'Size',
        key: 'size',
        width: 200,
        render: object => renderCell(object, 'TextCell', 'size')
    },
    {
        title: 'Privacy',
        key: 'privacy',
        width: 200,
        render: object => renderCell(object, 'TextCell', 'privacy')
    }
];
const editColumns = [
    { ...columns[0], width: 300 },
    { ...columns[1], width: 300 },
    columns[2],
    columns[3]
];
const tableinfos = [
    {
        title: 'Editable View',
        value: 'editView',
        columns: clone(editColumns)
    }
];
export { columns, tableinfos };
