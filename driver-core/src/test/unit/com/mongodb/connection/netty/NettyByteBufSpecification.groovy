/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.connection.netty

import com.mongodb.connection.SimpleBufferProvider
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class NettyByteBufSpecification extends Specification {
    def 'should put a byte'() {
        given:
        def buffer = provider.getBuffer(1024)

        when:
        buffer.put((byte) 42)
        buffer.flip()

        then:
        buffer.get() == 42

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'should put several bytes'() {
        given:
        def buffer = provider.getBuffer(1024)

        when:
        buffer.with {
            put((byte) 42)
            put((byte) 43)
            put((byte) 44)
            flip()
        }

        then:
        buffer.with {
            get() == 42
            get() == 43
            get() == 44
        }
        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'should put bytes at index'() {
        given:
        def buffer = provider.getBuffer(1024)

        when:
        buffer.with {
            put((byte) 0)
            put((byte) 0)
            put((byte) 0)
            put((byte) 0)
            put((byte) 43)
            put((byte) 44)
            put(0, (byte) 22)
            put(1, (byte) 23)
            put(2, (byte) 24)
            put(3, (byte) 25)
            flip()
        }

        then:
        buffer.with {
            get() == 22
            get() == 23
            get() == 24
            get() == 25
            get() == 43
            get() == 44
        }

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'when writing, remaining should be the number of bytes that can be written'() {
        when:
        def buffer = provider.getBuffer(1024)

        then:
        buffer.remaining() == 1024

        and:
        buffer.put((byte) 1)

        then:
        buffer.remaining() == 1023

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'when writing, hasRemaining should be true if there is still room to write'() {
        when:
        def buffer = provider.getBuffer(2)

        then:
        buffer.hasRemaining()

        and:
        buffer.put((byte) 1)

        then:
        buffer.hasRemaining()

        and:
        buffer.put((byte) 1)

        then:
        !buffer.hasRemaining()

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'should return NIO buffer with the same capacity and limit'() {
        given:
        def buffer = provider.getBuffer(36)

        when:
        def nioBuffer = buffer.asNIO()

        then:
        nioBuffer.limit() == 36
        nioBuffer.position() == 0
        nioBuffer.remaining() == 36

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }

    def 'should return NIO buffer with the same contents'() {
        given:
        def buffer = provider.getBuffer(1024)

        buffer.with {
            put((byte) 42)
            put((byte) 43)
            put((byte) 44)
            put((byte) 45)
            put((byte) 46)
            put((byte) 47)

            flip()
        }

        when:
        def nioBuffer = buffer.asNIO()

        then:
        nioBuffer.with {
            limit() == 6
            position() == 0
            get() == 42
            get() == 43
            get() == 44
            get() == 45
            get() == 46
            get() == 47
            remaining() == 0
        }

        where:
        provider << [new NettyBufferProvider(), new SimpleBufferProvider()]
    }
}
