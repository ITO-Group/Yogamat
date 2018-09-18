from flask import Flask, request, jsonify
app = Flask(__name__)

file = open('1-test','a+')
# Tell `app` that if someone asks for `/` (which is the main page)
# then run this function, and send back the return value
@app.route("/")
def hello():
    return "POST images to /mnistify"

@app.route("/", methods=['POST'])
def store():
    data = request.form['data']
    file.write(data)
    return jsonify({'result': 'ok'})

app.run(host='0.0.0.0', port=5000)