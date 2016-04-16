var path = require('path');

module.exports = {
  entry: [
    path.resolve(__dirname, 'src/main/javascript/main.js')
  ],
  output: {
    path: path.resolve(__dirname, 'target/scala-2.11/classes/public'),
    filename: 'index.js'
  },
  resolve: {
    extensions: ['', '.js', '.jsx']
  },
  module: {
    loaders: [
      {
        test: /\.jsx?$/,
        loader: 'babel-loader',
        query: {
          cacheDirectory: true,
          presets: ['react', 'es2015'],
          plugins: ["transform-function-bind"]
        },
        include: path.join(__dirname, 'src/main/javascript')
      }
    ]
  },
  devtool: 'eval'
}
