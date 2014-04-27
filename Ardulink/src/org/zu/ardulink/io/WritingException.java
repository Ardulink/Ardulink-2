/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/
package org.zu.ardulink.io;

public class WritingException extends Exception {

	private static final long serialVersionUID = 7380824605617847898L;

	public WritingException() {
		super();
	}

	public WritingException(String message, Throwable cause) {
		super(message, cause);
	}

	public WritingException(String message) {
		super(message);
	}

	public WritingException(Throwable cause) {
		super(cause);
	}
}
