import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:video_player/video_player.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: CameraStatusScreen(),
    );
  }
}

class CameraStatusScreen extends StatefulWidget {
  @override
  _CameraStatusScreenState createState() => _CameraStatusScreenState();
}

class _CameraStatusScreenState extends State<CameraStatusScreen> {
  static const platform = MethodChannel('com.example.usb_camera_app/usb');
  String _cameraStatus = "Checking...";
  VideoPlayerController? _controller;

  @override
  void initState() {
    super.initState();
    _checkCameraStatus();
    platform.setMethodCallHandler((call) async {
      if (call.method == 'updateCameraStatus') {
        setState(() {
          _cameraStatus = call.arguments ? "Camera is connected" : "No camera connected";
        });
      }
    });
  }

  Future<void> _checkCameraStatus() async {
    try {
      final bool isConnected = await platform.invokeMethod('checkCameraStatus');
      setState(() {
        _cameraStatus = isConnected ? "Camera is connected" : "No camera connected";
      });
    } on PlatformException catch (e) {
      setState(() {
        _cameraStatus = "Failed to get camera status: '${e.message}'.";
      });
    }
  }

  Future<void> _startVideoStream() async {
    try {
      await platform.invokeMethod('startVideoStream');
    } on PlatformException catch (e) {
      print("Failed to start video stream: '${e.message}'.");
    }
  }

  Future<void> _stopVideoStream() async {
    try {
      await platform.invokeMethod('stopVideoStream');
    } on PlatformException catch (e) {
      print("Failed to stop video stream: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('USB Camera App'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _cameraStatus,
              style: TextStyle(fontSize: 24),
            ),
            SizedBox(height: 20),
            ElevatedButton(
              onPressed: _startVideoStream,
              child: Text('Start Video Stream'),
            ),
            ElevatedButton(
              onPressed: _stopVideoStream,
              child: Text('Stop Video Stream'),
            ),
            // Aquí se podría añadir el VideoPlayer para mostrar el video
          ],
        ),
      ),
    );
  }
}
