import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: CameraScreen(),
    );
  }
}

class CameraScreen extends StatefulWidget {
  @override
  _CameraScreenState createState() => _CameraScreenState();
}

class _CameraScreenState extends State<CameraScreen> {
  static const platform = MethodChannel('usb_camera_channel');
  String _cameraStatus = 'Unknown';

  @override
  void initState() {
    super.initState();
    _checkPermissions();
  }

  Future<void> _checkPermissions() async {
    try {
      final bool hasPermission = await platform.invokeMethod('checkPermissions');
      if (hasPermission) {
        setState(() {
          _cameraStatus = 'Permission Granted';
        });
        _connectUsbCamera();
      } else {
        setState(() {
          _cameraStatus = 'Permission Denied';
        });
      }
    } on PlatformException catch (e) {
      setState(() {
        _cameraStatus = "Failed to get permission: '${e.message}'.";
      });
    }
  }

  Future<void> _connectUsbCamera() async {
    try {
      final String result = await platform.invokeMethod('connectUsbCamera');
      setState(() {
        _cameraStatus = result;
      });
    } on PlatformException catch (e) {
      setState(() {
        _cameraStatus = "Failed to connect to camera: '${e.message}'.";
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('USB Camera App'),
      ),
      body: Center(
        child: Text('Camera Status: $_cameraStatus'),
      ),
    );
  }
}
