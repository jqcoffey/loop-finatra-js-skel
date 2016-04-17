// main.js
var React = require('react');
var ReactDOM = require('react-dom');

class DataRow extends React.Component {
    render() {
        let data = JSON.parse(this.props.data);
        let row = [];
        data.forEach(_ => row.push(<td>{_}</td>));

        return (<tr>{row}</tr>)
    }
}

class DataTable extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            data: {
                data: [],
                columns: []
            }
        };
    }

    componentDidMount() {
        fetch('/query', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({'name': 'rates', 'params': []})
        }).then(res => res.json())
            .then(data => this.setState({data}));
    }

    render() {
        if (this.state.data) {
            let data = this.state.data.data;
            let columns = this.state.data.columns;

            let rows = [];
            data.forEach(_ => rows.push(<DataRow data={JSON.stringify(_)}/>));

            let header = [];
            columns.forEach(_ => header.push(<th>{_}</th>));

            return (
                <table>
                    <thead>
                    <tr>
                        {header}
                    </tr>
                    </thead>
                    <tbody>
                    {rows}
                    </tbody>
                </table>
            )

        }
    }
}

ReactDOM.render(<DataTable />, document.getElementById('example'));