import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:mjpegstreamer/commonProvider.dart';
import 'package:provider/provider.dart';

class MjpegSender extends StatefulWidget {
  const MjpegSender({super.key});

  @override
  State<MjpegSender> createState() => _MjpegSenderState();
}

class _MjpegSenderState extends State<MjpegSender> {
  static const platform = MethodChannel('kr.co.brownyc.mjpegstreamer/camera');

  final _wsController = TextEditingController(text: "ws://192.168.0.102:8090/ws");
  TextEditingController resolutionController = TextEditingController(text: "640x480");
  TextEditingController jpegQualityController = TextEditingController(text: "60");

  bool _isStreaming = false;
  bool isBusy = false;

  Future<void> _toggleStream() async {
    print("⚠️ onPressed: _isStreaming=${_isStreaming.toString()}");
    // _isStreaming = await platform.invokeMethod('isStreaming');
    try {
      if (!_isStreaming) {
        final res = resolutionController.text.split('x');
        final width = int.tryParse(res[0]) ?? 640;
        final height = int.tryParse(res[1]) ?? 480;
        final jpegQ = int.tryParse(jpegQualityController.text) ?? 60;
        await platform.invokeMethod('startCamera', {
          'ws_url': _wsController.text.trim(),
          'width': width,
          'height': height,
          'jpeg_quality': jpegQ,
        });
        // setState(() => _isStreaming = true);
        isBusy = true;
      } else {
        await platform.invokeMethod('stopCamera');
        // setState(() => _isStreaming = false);
      }
      setState(() => _isStreaming = !_isStreaming);
    } catch (e) {
      print("⚠️ error: $e");
    } finally {
      isBusy = false;
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = Provider.of<commonProvider>(context);
    return Column(
      children: [
        TextField(
          controller: _wsController,
          decoration: InputDecoration(labelText: "WebSocket URL"),
        ),
        TextField(
          controller: resolutionController,
          decoration: InputDecoration(labelText: 'Resolution (e.g., 640x480)'),
        ),
        TextField(
          controller: jpegQualityController,
          decoration: InputDecoration(labelText: 'JPEG Quality (0-100)'),
          keyboardType: TextInputType.number,
        ),
        SizedBox(height: 16),
        ElevatedButton(
          onPressed: () {
            print("⚠️ onPressed: isBusy=${isBusy.toString()}");
            provider.setUrl(_wsController.text);
            isBusy ? null : _toggleStream();
          },
          child: Text(_isStreaming ? "송출 중단" : "송출 시작"),
        ),
      ],
    );
  }
}
