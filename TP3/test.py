import sys
import threading
import urllib2
import time

server_url = "http://132.207.12.85:8080/?nom=inf8480"

temps_exec = 0
mutex = threading.Lock()
num_reponses = 0


def do_get():

    start_time = time.time()
    global temps_exec, num_reponses

    try:
        content = urllib2.urlopen(server_url)
        content.read()
        content.close()
    except:
        sys.exit(1)

    end_time = time.time()

    mutex.acquire()
    temps_exec += round((end_time - start_time) * 1000)
    num_reponses += 1
    mutex.release()


def run():
    req_threads = []

    for i in range(0, 30):
        req_threads.append(threading.Thread(target=do_get))
        req_threads[-1].start()
    for thread in req_threads:
        thread.join()

    print('nb reponses recues: {}'.format(num_reponses),'nb rejets: {}'.format(30 - num_reponses), 'temps d\'execution : {} ms'.format(round(temps_exec / num_reponses)))


def main():
    print('sending request to server...')
    run()

if __name__ == "__main__":
    main()
