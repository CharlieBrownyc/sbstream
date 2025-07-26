import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:convert';
import 'dart:io';

import 'package:mjpegstreamer/commonProvider.dart';
import 'package:provider/provider.dart';

class MjpegViewer extends StatefulWidget {
  const MjpegViewer({super.key});

  @override
  _MjpegViewerState createState() => _MjpegViewerState();
}

class _MjpegViewerState extends State<MjpegViewer> {
  final TextEditingController _urlController =
  TextEditingController(text: "ws://192.168.0.10:8090/ws");
  WebSocket? _socket;
  Uint8List? _latestFrame;
  bool isConnected = false;

  void _connect() async {
    try {
      _socket = await WebSocket.connect(_urlController.text.trim());
      setState(() => isConnected = true);

      _socket!.listen((msg) {
        final parsed = jsonDecode(msg);
        if (parsed['type'] == 'video') {
          final base64Str = (parsed['frame'] as String).split(',').last;
          final bytes = base64Decode(base64Str);
          setState(() => _latestFrame = bytes);
        }
      }, onDone: _disconnect);
    } catch (e) {
      print("⚠️ WebSocket error: $e");
      _disconnect();
    }
  }

  void _disconnect() {
    _socket?.close();
    setState(() {
      isConnected = false;
      _socket = null;
    });
  }

  @override
  void dispose() {
    _disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final url = Provider.of<commonProvider>(context).url;
    _urlController.text = url;
    return Scaffold(
      appBar: AppBar(title: Text("🖼️ MJPEG Viewer")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            TextField(
              controller: _urlController,
              decoration: InputDecoration(labelText: "WebSocket URL"),
            ),
            SizedBox(height: 16),
            Row(
              children: [
                ElevatedButton(
                  onPressed: isConnected ? null : _connect,
                  child: Text("접속"),
                ),
                SizedBox(width: 16),
                ElevatedButton(
                  onPressed: isConnected ? _disconnect : null,
                  child: Text("종료"),
                ),
              ],
            ),
            SizedBox(height: 20),
            _latestFrame != null
                ? Image.memory(_latestFrame!)
                : Text("아직 프레임 없음"),
          ],
        ),
      ),
    );
  }
}
